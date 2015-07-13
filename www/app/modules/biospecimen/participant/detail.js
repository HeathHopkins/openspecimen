
angular.module('os.biospecimen.participant.detail', ['os.biospecimen.models'])
  .controller('ParticipantDetailCtrl', function(
    $scope, $q, cpr, visits, 
    CollectionProtocol, SpecimenLabelPrinter, PvManager, 
    RegisterToNewCpsHolder, DeleteUtil) {

    function loadPvs() {
      $scope.genders = PvManager.getPvs('gender');
      $scope.ethnicities = PvManager.getPvs('ethnicity');
      $scope.vitalStatuses = PvManager.getPvs('vital-status');
      $scope.races = PvManager.getPvs('race');

      var siteNames = cpr.getMrnSites();
      $scope.cpsForReg = [];
      CollectionProtocol.listForRegistrations(siteNames).then(
        function(cps) {
          var registeredCps = cpr.participant.registeredCps;
          for (var i = 0; i < cps.length; ++i) {
            if (registeredCps.indexOf(cps[i].shortTitle) == -1) {
              $scope.cpsForReg.push(cps[i]);
            }
          }
        }
      );

      RegisterToNewCpsHolder.setCpList($scope.cpsForReg);
    }

    function init() {
      $scope.cpr = cpr;
      loadPvs();
    }

    $scope.editCpr = function(property, value) {
      var d = $q.defer();
      d.resolve({});
      return d.promise;
    }

    $scope.pmiText = function(pmi) {
      return pmi.mrn + " (" + pmi.siteName + ")";
    }

    $scope.printSpecimenLabels = function(visitDetail) {
      SpecimenLabelPrinter.printLabels(visitDetail);
    }

    $scope.deleteReg = function() {
      DeleteUtil.delete($scope.cpr, {onDeleteState: 'participant-list'});
    }

    /**
     * Add visit logic
     */
    $scope.addVisitIdx = -1;
    $scope.rptVisitIdx = -1;

    $scope.showAddVisit = function(visit, index) {
      $scope.addVisitIdx = index;
      $scope.rptVisitIdx = -1;
      $scope.visitToAdd = visit;
    };

    $scope.revertAddVisit = function() {
      $scope.addVisitIdx = $scope.rptVisitIdx = -1;
      $scope.visitToAdd = {};
    };

    $scope.showRepeatVisit = function(visit, index) {
      $scope.addVisitIdx = -1;
      $scope.rptVisitIdx = index;
      $scope.visitToAdd = angular.copy(visit);
    };

    $scope.showMissedVisit = function(visit, index) {
      $scope.addVisitIdx = index;
      $scope.rptVisitIdx = -1;
      $scope.visitToAdd = angular.extend(angular.copy(visit), {status: 'Missed Collection'});
    };

    init();
  });
