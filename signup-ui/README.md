# E-Signet Sign Up Web Application

## Description

The E-Signet Sign Up web application is a platform allowing L1 and L2 users in Cambodia to register their identities.

## Local Development

### First Setup

To install relevant npm packages, run the following script:

```bash
npm install
```

### Running Locally

#### Browser

During development, we were using Chrome for debugging. You are recommended to open Chrome with --disable-web-security mode to avoid CORS errors.

Mac users use the following command:

```bash
open -na Google\ Chrome --args --user-data-dir=/tmp/temporary-chrome-profile-dir --disable-web-security
```

Ubuntu users use the following command:

```bash
google-chrome-stable --user-data-dir="~/ dev session" --disable-web-security
```

#### Scripts

To start the web application, run the following script:

```bash
npm start
```

This opens [http://localhost:3000](http://localhost:3000) to view it in the browser. The page will reload if you make edits. You will also see any lint errors in the console.

#### Environments

We provide you with `.env.example` file. You can copy and paste the variables described into a self-created `.env.local` file, replacing the required values with your own.
