const React = require('react');

module.exports = React.createClass({
  render: function() {
    return (
      <div className="panel flex" id={this.props.id}></div>
    );
  }
});
