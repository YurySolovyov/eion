const { createStore, compose, applyMiddleware } = require('redux');
const thunk = require('redux-thunk').default;
const rootReducer = require('../reducers/index');
const DirectoryService = require('../services/directory-service');

const initialPath = DirectoryService.getInitialPath();

const defaultState = {
  panels: {
    left: {
      currentPath: initialPath,
      directoryItems: []
    },
    right: {
      currentPath: initialPath,
      directoryItems: []
    },
    current: 'right'
  }
};

const store = createStore(rootReducer, defaultState, applyMiddleware(thunk));
DirectoryService.iniialize(store, initialPath, initialPath);

module.exports = store;
