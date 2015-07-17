package com.krishagni.catissueplus.core.importer.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import au.com.bytecode.opencsv.CSVWriter;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.errors.ParameterizedError;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.service.ConfigurationService;
import com.krishagni.catissueplus.core.common.util.AuthUtil;
import com.krishagni.catissueplus.core.common.util.ConfigUtil;
import com.krishagni.catissueplus.core.importer.domain.ImportJob;
import com.krishagni.catissueplus.core.importer.domain.ImportJob.Status;
import com.krishagni.catissueplus.core.importer.domain.ImportJob.Type;
import com.krishagni.catissueplus.core.importer.domain.ImportJobErrorCode;
import com.krishagni.catissueplus.core.importer.domain.ObjectSchema;
import com.krishagni.catissueplus.core.importer.events.ImportDetail;
import com.krishagni.catissueplus.core.importer.events.ImportJobDetail;
import com.krishagni.catissueplus.core.importer.events.ImportObjectDetail;
import com.krishagni.catissueplus.core.importer.events.ObjectSchemaCriteria;
import com.krishagni.catissueplus.core.importer.repository.ImportJobDao;
import com.krishagni.catissueplus.core.importer.repository.ListImportJobsCriteria;
import com.krishagni.catissueplus.core.importer.services.ImportService;
import com.krishagni.catissueplus.core.importer.services.ObjectImporter;
import com.krishagni.catissueplus.core.importer.services.ObjectImporterFactory;
import com.krishagni.catissueplus.core.importer.services.ObjectReader;
import com.krishagni.catissueplus.core.importer.services.ObjectSchemaFactory;

public class ImportServiceImpl implements ImportService {
	private ConfigurationService cfgSvc;
	
	private ImportJobDao importJobDao;
	
	private ThreadPoolTaskExecutor taskExecutor;
	
	private ObjectSchemaFactory schemaFactory;
	
	private ObjectImporterFactory importerFactory;
	
	private PlatformTransactionManager transactionManager;
	
	private TransactionTemplate txTmpl;
	
	private MessageSource messageSource;
	
	public void setCfgSvc(ConfigurationService cfgSvc) {
		this.cfgSvc = cfgSvc;
	}
	
	public void setImportJobDao(ImportJobDao importJobDao) {
		this.importJobDao = importJobDao;
	}
	
	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setSchemaFactory(ObjectSchemaFactory schemaFactory) {
		this.schemaFactory = schemaFactory;
	}

	public void setImporterFactory(ObjectImporterFactory importerFactory) {
		this.importerFactory = importerFactory;
	}
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		this.txTmpl = new TransactionTemplate(this.transactionManager);
		this.txTmpl.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	}
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<List<ImportJobDetail>> getImportJobs(RequestEvent<ListImportJobsCriteria> req) {
		try {
			ListImportJobsCriteria crit = req.getPayload();
			if (!AuthUtil.isAdmin()) {
				crit.userId(AuthUtil.getCurrentUser().getId());
			}
			
			List<ImportJob> jobs = importJobDao.getImportJobs(crit);			
			return ResponseEvent.response(ImportJobDetail.from(jobs));
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<ImportJobDetail> getImportJob(RequestEvent<Long> req) {
		try {
			ImportJob job = getImportJob(req.getPayload());
			return ResponseEvent.response(ImportJobDetail.from(job));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<String> getImportJobFile(RequestEvent<Long> req) {
		try {
			ImportJob job = getImportJob(req.getPayload());
			File file = new File(getJobOutputFilePath(job.getId()));
			if (!file.exists()) {
				return ResponseEvent.userError(ImportJobErrorCode.OUTPUT_FILE_NOT_CREATED);
			}
			
			return ResponseEvent.response(file.getAbsolutePath());
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
		
	@Override
	public ResponseEvent<String> uploadImportJobFile(RequestEvent<InputStream> req) {
		OutputStream out = null;
		
		try {
			//
			// 1. Ensure import directory is present
			//
			String importDir = getImportDir();			
			new File(importDir).mkdirs();
			
			//
			// 2. Generate unique file ID
			//
			String fileId = UUID.randomUUID().toString();
			
			//
			// 3. Copy uploaded file to import directory
			//
			InputStream in = req.getPayload();			
			out = new FileOutputStream(importDir + File.separator + fileId);
			IOUtils.copy(in, out);
			
			return ResponseEvent.response(fileId);			
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}	
	
	@Override
	@PlusTransactional
	public ResponseEvent<ImportJobDetail> importObjects(RequestEvent<ImportDetail> req) {
		try {
			ImportDetail detail = req.getPayload();
			ImportJob job = createImportJob(detail);						
			importJobDao.saveOrUpdate(job, true);
			
			//
			// Set up file in job's directory
			//
			String inputFile = getFilePath(detail.getInputFileId());
			createJobDir(job.getId());
			moveToJobDir(inputFile, job.getId());
			
			taskExecutor.submit(new ImporterTask(AuthUtil.getAuth(), job));
			return ResponseEvent.response(ImportJobDetail.from(job));
		} catch (Exception e) {
			return ResponseEvent.serverError(e);			
		}		
	}
	
	@Override
	public ResponseEvent<String> getInputFileTemplate(RequestEvent<ObjectSchemaCriteria> req) {
		try {
			ObjectSchemaCriteria detail = req.getPayload();
			ObjectSchema schema = schemaFactory.getSchema(detail.getObjectType(), detail.getParams());
			if (schema == null) {
				return ResponseEvent.userError(ImportJobErrorCode.OBJ_SCHEMA_NOT_FOUND, detail.getObjectType());
			}
			
			return ResponseEvent.response(ObjectReader.getSchemaFields(schema));
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	
	private ImportJob getImportJob(Long jobId) {
		User currentUser = AuthUtil.getCurrentUser();
		
		ImportJob job = importJobDao.getById(jobId);
		if (job == null) {
			throw OpenSpecimenException.userError(ImportJobErrorCode.NOT_FOUND);
		}
		
		if (!currentUser.isAdmin() && !currentUser.equals(job.getCreatedBy())) {
			throw OpenSpecimenException.userError(ImportJobErrorCode.ACCESS_DENIED);
		}
		
		return job;
	}
	
	private String getDataDir() {
		return cfgSvc.getDataDir();
	}
	
	private String getImportDir() {
		return getDataDir() + File.separator + "bulk-import";
	}
	
	private String getFilePath(String fileId) { 
		 return getImportDir() + File.separator + fileId;
	}
	
	private String getJobsDir() {
		return getDataDir() + File.separator + "bulk-import" + File.separator + "jobs";
	}
	
	private String getJobDir(Long jobId) {
		return getJobsDir() + File.separator + jobId;
	}
	
	private String getJobOutputFilePath(Long jobId) {
		return getJobDir(jobId) + File.separator + "output.csv";
	}
	
	private boolean createJobDir(Long jobId) {
		return new File(getJobDir(jobId)).mkdirs();
	}
	
	private boolean moveToJobDir(String file, Long jobId) {
		File src = new File(file);
		File dest = new File(getJobDir(jobId) + File.separator + "input.csv");
		return src.renameTo(dest);		
	}
	
	private ImportJob createImportJob(ImportDetail detail) { // TODO: ensure checks are done
		ImportJob job = new ImportJob();
		job.setCreatedBy(AuthUtil.getCurrentUser());
		job.setCreationTime(Calendar.getInstance().getTime());
		job.setName(detail.getObjectType());
		job.setStatus(Status.IN_PROGRESS);
		job.setParams(detail.getObjectParams());
		
		String importType = detail.getImportType();
		job.setType(StringUtils.isBlank(importType) ? Type.CREATE : Type.valueOf(importType));		
		return job;		
	}

	private String getMessage(ParameterizedError error) {
		return messageSource.getMessage(
				error.error().code().toLowerCase(), 
				error.params(), 
				Locale.getDefault());
	}
	
	private String getMessage(OpenSpecimenException ose) {
		StringBuilder errorMsg = new StringBuilder();
		if (!ose.getErrors().isEmpty()) {
			for (ParameterizedError pe : ose.getErrors()) {
				errorMsg.append(getMessage(pe)).append(", ");
			}
			errorMsg.delete(errorMsg.length() - 2, errorMsg.length());
		} else if (ose.getException() != null) {
			errorMsg.append(ose.getException().getMessage());
		} else {
			errorMsg.append("Unknown error");
		}
		
		return errorMsg.toString();		
	}
	
	private class ImporterTask implements Runnable {
		private Authentication auth;
		
		private ImportJob job;
		
		public ImporterTask(Authentication auth, ImportJob job) {
			this.auth = auth;
			this.job = job;			
		}

		@Override
		public void run() {
			SecurityContextHolder.getContext().setAuthentication(auth);			
			ObjectSchema   schema   = schemaFactory.getSchema(job.getName(), job.getParams());
			ObjectImporter<Object, Object> importer = importerFactory.getImporter(job.getName());
			
			ObjectReader objReader = null;
			CSVWriter csvWriter = null;
			long totalRecords = 0, failedRecords = 0;
			try {
				String filePath = getJobDir(job.getId()) + File.separator + "input.csv";
				objReader = new ObjectReader(
						filePath, schema, 
						ConfigUtil.getInstance().getDeDateFmt(),
						ConfigUtil.getInstance().getTimeFmt());
				
				List<String> columnNames = objReader.getCsvColumnNames();
				columnNames.add("OS_IMPORT_STATUS");
				columnNames.add("OS_ERROR_MESSAGE");
				
				csvWriter = getOutputCsvWriter(job);
				csvWriter.writeNext(columnNames.toArray(new String[0]));
				
				while (true) {
					String errMsg = null;					
					try {
						Object object = objReader.next();
						if (object == null) {
							break;
						}
											
						errMsg = importObject(importer, object, job.getParams());
					} catch (OpenSpecimenException ose) {						
						errMsg = getMessage(ose);						
					}
					
					++totalRecords;
					
					List<String> row = objReader.getCsvRow();
					if (StringUtils.isNotBlank(errMsg)) {
						row.add("FAIL");
						row.add(errMsg);
						++failedRecords;
					} else {
						row.add("SUCCESS");
						row.add("");
					}
					
					csvWriter.writeNext(row.toArray(new String[0]));
					if (totalRecords % 25 == 0) {
						saveJob(totalRecords, failedRecords, Status.IN_PROGRESS);
					}					
				}
				
				saveJob(totalRecords, failedRecords, Status.COMPLETED);
			} catch (Exception e) {
				e.printStackTrace();
				saveJob(totalRecords, failedRecords, Status.FAILED);
			} finally {
				IOUtils.closeQuietly(objReader);
				closeQueitly(csvWriter);
			}
		}
		
		private String importObject(final ObjectImporter<Object, Object> importer, Object object, Map<String, Object> params) {
			try {
				ImportObjectDetail<Object> detail = new ImportObjectDetail<Object>();
				detail.setCreate(job.getType() == Type.CREATE);
				detail.setObject(object);
				detail.setParams(params);
				
				final RequestEvent<ImportObjectDetail<Object>> req = new RequestEvent<ImportObjectDetail<Object>>(detail);
				ResponseEvent<Object> resp = txTmpl.execute(
						new TransactionCallback<ResponseEvent<Object>>() {
							@Override
							public ResponseEvent<Object> doInTransaction(TransactionStatus status) {
								ResponseEvent<Object> resp = importer.importObject(req);
								if (!resp.isSuccessful()) {
									status.setRollbackOnly();
								}
								
								return resp;
							}
						});
				
				if (resp.isSuccessful()) {
					return null;
				} else {
					return getMessage(resp.getError());
				}				
			} catch (Exception e) {
				if (StringUtils.isBlank(e.getMessage())) {					
					return "Internal Server Error";
				} else {
					return e.getMessage();
				}
			}
		}
				
		private void saveJob(long totalRecords, long failedRecords, Status status) {
			job.setTotalRecords(totalRecords);
			job.setFailedRecords(failedRecords);
			job.setStatus(status);
			
			if (status == Status.COMPLETED || status == Status.FAILED) {
				job.setEndTime(Calendar.getInstance().getTime());
			}
			
			txTmpl.execute(new TransactionCallback<Void>() {
				@Override
				public Void doInTransaction(TransactionStatus status) {
					importJobDao.saveOrUpdate(job);
					return null;
				}
			});
		}

		private CSVWriter getOutputCsvWriter(ImportJob job) 
		throws IOException {
			return new CSVWriter(new FileWriter(getJobOutputFilePath(job.getId())));
		}
				
		private void closeQueitly(CSVWriter writer) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
											
				}
			}
		}
	}
}
