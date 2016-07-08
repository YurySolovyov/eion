'use strict';
const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const NodeEnvironmentPlugin = webpack.NodeEnvironmentPlugin;

const config = {
  context: __dirname,
  entry: './src/app.js',

  target: 'electron',

  output: {
    path: path.join(__dirname, '/src/compiled'),
    publicPath: 'http://localhost:9090/dist/',
    filename: 'app.js'
  },

  cache: true,

  module: {
    loaders: [{
      test: /\.styl$/,
      loader: ExtractTextPlugin.extract("style-loader", "css-loader!stylus-loader")
    }, {
      test: /\.css$/,
      loader: ExtractTextPlugin.extract("style-loader", "css-loader")
    }, {
      test: /(\.jsx|app.js)$/,
      exclude: /node_modules/,
      loader: 'babel',
      query: {
        presets: ['react']
      }
    }, {
      test: /\.(eot|woff|ttf)([\?]?.*)$/,
      loader: 'url-loader?name=fonts/[name].[ext]',
    }, {
      test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
      loader: 'url?mimetype=image/svg+xml',
      include: /node_modules/
    }]
  },

  resolve: {
    alias: {
      basscss: path.join(__dirname, 'node_modules/basscss/css/basscss.css')
    },
    extensions: ["", ".js", ".jsx"]
  },

  plugins: [
    new CopyWebpackPlugin([
      { from: './src/index.html' },
      { from: './src/index.js' },
      { from: './package.json' },
    ], {
      copyUnmodified: true
    }),
    new NodeEnvironmentPlugin(),
    new ExtractTextPlugin('style.css', { allChunks: true })
  ],
};

module.exports = config;
