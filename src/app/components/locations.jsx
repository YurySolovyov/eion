const React = require('react');
const Location = require('./location');

module.exports = React.createClass({
  renderItems: function(items, currentPath) {
    const navigate = this.props.navigate;
    return items.map((item, index) => {
      return (
        <Location
          {...item}
          key={item.name}
          index={index}
          currentPath={currentPath}
          navigate={navigate} />
      );
    })
  },

  render: function() {
    const panel = this.props[this.props.id+"Panel"];
    return (
      <div className="locations flex m2">
        {this.renderItems(this.props.locations, panel.currentPath)}
      </div>
    );
  }
});
