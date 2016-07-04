'use strict';

require('basscss');
require('./styles/styles.styl');

const React = require('react');
const ReactDOM = require('react-dom');
const { Route, IndexRoute, Router, hashHistory } = require('react-router');
const App = require('./app/components/app.jsx');

const routes = (
  <Route component={ App } path='/' />
);

ReactDOM.render(
  <Router routes={ routes } history={ hashHistory } />,
  document.getElementById('root')
);
