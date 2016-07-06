const React = require('react');

module.exports = React.createClass({
  render: function() {
    return (
      <div id="container">
        { React.cloneElement(this.props.children, this.props) }
      </div>
    );
  }
});
