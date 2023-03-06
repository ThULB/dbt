const path = require("path");
const webpack = require("webpack");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserPlugin = require("terser-webpack-plugin");

const pkg = require("./package.json");
const pomSrcPath = "${basedir}";
const srcPath = (pomSrcPath && pomSrcPath.indexOf("basedir") === -1 && pomSrcPath || "../../..") + "/src/main/media";

const pomBuildPath = "${project.build.directory}";
const buildPath = pomBuildPath && pomBuildPath.indexOf("project.build.directory") === -1 && pomBuildPath || "../../../target";

const version = "${project.version}";
const banner = `
${pkg.name} ${version && ("v" + version) || pkg.version}
Homepage: ${pkg.homepage}
Copyright 2013-${(new Date()).getFullYear()} ${pkg.author} and others
Licensed under ${pkg.license}
`;

module.exports = {
  mode: "production",
  target: "web",
  devtool: "source-map",
  plugins: [
    new MiniCssExtractPlugin({
      filename: "[name].min.css",
      chunkFilename: "[id].css",
      ignoreOrder: false,
    }),
    new webpack.BannerPlugin({ banner })
  ],
  entry: {
    embed: [srcPath + "/ts/embed.ts", srcPath + "/scss/embed.scss"],
    player: [srcPath + "/ts/player.ts", srcPath + "/scss/player.scss"],
    "player-small": [srcPath + "/ts/player-small.ts", srcPath + "/scss/player-small.scss"]
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: require.resolve("awesome-typescript-loader"),
            options: {
              "useBabel": true,
              "babelOptions": {
                "babelrc": false,
                "presets": [
                  ["@babel/preset-env", { "targets": "last 2 versions, ie 11", "modules": false }]
                ]
              },
              "babelCore": "@babel/core",
            }
          },
        ],
      },
      {
        test: /.scss$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
          },
          {
            loader: require.resolve("css-loader"),
            options: {
              sourceMap: true
            }
          },
          {
            loader: require.resolve("sass-loader"),
          }
        ]
      },
      {
        test: /\.svg$/,
        use: [
          {
            loader: "url-loader"
          },
          {
            loader: "svgo-loader",
            options: {
              plugins: [
                { removeTitle: true },
                { convertColors: { shorthex: false } },
                { convertPathData: false }
              ]
            }
          }
        ]
      }
    ],
  },
  resolve: {
    extensions: [".tsx", ".ts", ".js"],
    modules: [path.resolve(__dirname, "node_modules")]
  },
  output: {
    filename: "[name].min.js",
    path: path.resolve(__dirname, buildPath + "/classes/META-INF/resources/dbt/assets/media"),
  },
  optimization: {
    minimize: true,
    minimizer: [
      new TerserPlugin({
        parallel: true,
        test: /\.js(\?.*)?$/i,
        terserOptions: {
          sourceMap: true,
        }
      }),
    ],
  }
};