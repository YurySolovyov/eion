const { createStore, compose } = require('redux');
const rootReducer = require('../reducers/index');
const DirectoryService = require('../services/directory-service');

const initialPath = DirectoryService.getInitialPath();
const initialItems = DirectoryService.getDirectoryItems(initialPath);

const defaultState = {
  panels: {
    left: {
      currentPath: initialPath,
      directoryItems: initialItems
    },
    right: {
      currentPath: initialPath,
      directoryItems: initialItems
    },
    current: 'right'
  }
};

const store = createStore(rootReducer, defaultState);

module.exports = store;
