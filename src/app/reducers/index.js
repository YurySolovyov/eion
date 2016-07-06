const { combineReducers } = require('redux');
const { routerReducer } = require('react-router-redux');

const panels = require('./panels');

const rootReducer = combineReducers({ panels, routing: routerReducer })

module.exports = rootReducer;
