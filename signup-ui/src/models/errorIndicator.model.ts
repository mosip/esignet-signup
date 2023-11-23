interface IErrorIndicator {
  prefix: string;
  errorCode: string;
  defaultMsg: string;
  i18nKeyPrefix: string;
  customClass: string;
}

export default IErrorIndicator;
