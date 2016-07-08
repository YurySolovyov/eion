'use strict';

require('basscss');
require('./styles/styles.styl');
require('font-awesome/css/font-awesome.css');

const React = require('react');
const { render } = require('react-dom');
const { Route, IndexRoute, Router, hashHistory } = require('react-router');
const { Provider } = require('react-redux');

const App = require('./app/components/app');
const Panels = require('./app/components/panels');

const store = require('./app/stores/store');

render(
  <Provider store={store}>
    <Router history={hashHistory}>
      <Route path='/' component={ App }>
        <IndexRoute component={ Panels } />
      </Route>
    </Router>
  </Provider>,
  document.getElementById('root')
);
