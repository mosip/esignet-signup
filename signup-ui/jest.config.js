const esModules = [].join("|");

module.exports = {
  preset: "ts-jest",
  transformIgnorePatterns: [`node_modules/(?!${esModules})/`],
  testTimeout: 5000,
};
