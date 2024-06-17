/* eslint-env node */
const CracoAlias = require("craco-alias");
const merge = require("lodash/merge");
const customJestConfig = require("./jest.config");

module.exports = {
  jest: {
    configure: (jestConfig) => {
      const newConfig = merge({}, jestConfig, customJestConfig);
      return newConfig;
    },
  },
  plugins: [
    {
      plugin: CracoAlias,
      options: {
        source: "tsconfig",
        // baseUrl SHOULD be specified
        // plugin does not take it from tsconfig
        baseUrl: ".",
        // tsConfigPath should point to the file where "baseUrl" and "paths" are specified
        tsConfigPath: "./tsconfig.paths.json",
        unsafeAllowModulesOutsideOfSrc: true,
      },
    },
  ],
};
