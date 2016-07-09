const React = require('react');
const FontAwesome = require('react-fontawesome');

module.exports = React.createClass({
  handleClick: function() {
    this.props.navigate(this.props.path + '//');
  },
  render: function() {
    const active = this.props.currentPath.toLowerCase().startsWith(this.props.path);
    return (
      <span className={`location pr2 ml1 ${active ? 'active': ''}`} onClick={this.handleClick} key={this.props.name}>
        <FontAwesome name='hdd-o' className='px1' />
        {this.props.name}
      </span>
    );
  }
});
