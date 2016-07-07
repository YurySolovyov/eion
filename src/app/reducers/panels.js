const path = __non_webpack_require__('path');
const match = require('pamach');
const DirectoryService = require('../services/directory-service');

module.exports = function panels(state = {}, action) {
  return match(action.type) ({
    NAVIGATE: () => {
      const currentPanel = state.current;
      const currentPath = state[currentPanel].currentPath;
      const newPath = path.resolve(currentPath, action.path);

      return Object.assign({}, state, {
        [currentPanel]: {
          currentPath: newPath,
          directoryItems: DirectoryService.getDirectoryItems(newPath)
        }
      });
    },

    OPEN: () => {
      // does not work yet, needs a way to tell if path is dir.
      const currentPanel = state.current;
      const currentPath = state[currentPanel].currentPath;
      const openPath = path.join(currentPath, action.item);
      DirectoryService.openPath(openPath);

      return state;
    },

    ACTIVATE_PANEL: () => {
      return Object.assign({}, state, { current: action.id });
    },

    default: () => {
      return state;
    }
  });
};
