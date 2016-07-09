const { createStore, compose, applyMiddleware } = require('redux');
const thunk = require('redux-thunk').default;
const rootReducer = require('../reducers/index');
const DirectoryService = require('../services/directory-service');
const LocationsService = require('../services/locations-service');

const initialPath = DirectoryService.getInitialPath();

const defaultDirectoryItems = {
  files: [],
  directories: []
};

const defaultState = {
  panels: {
    left: {
      currentPath: initialPath,
      directoryItems: defaultDirectoryItems
    },
    right: {
      currentPath: initialPath,
      directoryItems: defaultDirectoryItems
    },
    current: 'right',
    locations: []
  },
};

const store = createStore(rootReducer, defaultState, applyMiddleware(thunk));
DirectoryService.initialize(store, initialPath, initialPath);
LocationsService.initialize(store);

module.exports = store;
