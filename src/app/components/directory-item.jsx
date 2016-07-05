const React = require('react');

module.exports = React.createClass({
  render: function() {
    return (
      <div className="directory-item p1">{this.props.label}</div>
    );
  }
});
