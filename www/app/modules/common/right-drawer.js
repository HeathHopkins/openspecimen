angular.module('openspecimen')
  .directive('osRightDrawer', function(osRightDrawerSvc) {
    return {
      restrict: 'A',

      link: function(scope, element, attrs) {
        element.addClass('os-right-drawer');
        element.removeAttr('os-right-drawer');

        osRightDrawerSvc.setDrawer(element);
      }
    };
  })
  .directive('osRightDrawerToggle', function(osRightDrawerSvc) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        element.on('click', function() {
          osRightDrawerSvc.toggle();
        });
      }
    };
  })
  .factory('osRightDrawerSvc', function() {
    var drawerEl = undefined;
    
    function setCardsViewWidth(width) {
      var cardsDiv = drawerEl.parent().find("div.os-cards, div.os-list-container");
      cardsDiv.css('width', width);
    }

    function open() {
      if (drawerEl.hasClass('active')) {
        return;
      }

      drawerEl.addClass('active');
      drawerEl.find('input, textArea, select, button').filter(':visible:first').focus();
      drawerEl.scope().$emit('osRightDrawerOpen');
    }

    function close() {
      if (!drawerEl.hasClass('active')) {
        return;
      }

      drawerEl.removeClass('active');
      drawerEl.scope().$emit('osRightDrawerClose');
    }
       
    return {
      setDrawer: function(drawer) {
        drawerEl = drawer;
      },

      toggle: function() {
        if (drawerEl.hasClass('active')) {
          close();
        } else {
          open();
        }
      },

      open: open,

      close: close
    }
  });
