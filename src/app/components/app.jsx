const { bindActionCreators } = require('redux');
const { connect } = require('react-redux');
const actionCreators = require('../actions/action-creators');

const Main = require('./main');

const mapStateToProps = function(state) {
  return {
    leftPanel: state.panels.left,
    rightPanel: state.panels.right,
    currentPanel: state.panels.current
  };
};

const mapDispatchToProps = function(dispatch) {
  return bindActionCreators(actionCreators, dispatch);
};

module.exports = connect(mapStateToProps, mapDispatchToProps)(Main);
