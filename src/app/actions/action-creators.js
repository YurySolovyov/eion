const path = require('path');
const DirectoryService = require('../services/directory-service');

const navigate = function(navigation) {
  return function(dispatch, getState) {

    const state = getState();
    const currentPath = state.panels[state.panels.current].currentPath;
    const newPath = path.resolve(currentPath, navigation);

    DirectoryService.getDirectoryItems(newPath).then(function(items) {
      dispatch({
        type: 'NAVIGATE',
        path: newPath,
        items
      });
    });
  };
};

const open = function(item) {
  return function(dispatch) {
    dispatch({
      type: 'OPEN',
      item
    });
  };
};

const activatePanel = function(id) {
  return function(dispatch) {
    dispatch({
      type: 'ACTIVATE_PANEL',
      id
    });
  };
};

module.exports = {
  navigate,
  open,
  activatePanel
};
