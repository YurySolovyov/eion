const path = require('path');
const match = require('pamach');
const DirectoryService = require('../services/directory-service');

module.exports = function panels(state = {}, action) {
  return match(action.type) ({
    NAVIGATE: () => {
      const currentPanel = action.panel || state.current;
      const newPath = action.path;

      return Object.assign({}, state, {
        [currentPanel]: {
          currentPath: newPath,
          directoryItems: action.items
        }
      });
    },

    OPEN: () => {
      const currentPanel = state.current;
      const currentPath = state[currentPanel].currentPath;
      const openPath = path.join(currentPath, action.item);
      DirectoryService.openPath(openPath);

      return state;
    },

    ACTIVATE_PANEL: () => {
      return Object.assign({}, state, { current: action.id });
    },

    UPDATE_LOCATIONS: () => {
      return Object.assign({}, state, { locations: action.locations });
    },

    default: () => {
      return state;
    }
  });
};
