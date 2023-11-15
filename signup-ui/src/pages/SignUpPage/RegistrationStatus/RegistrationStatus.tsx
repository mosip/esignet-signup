import StatusPageTemplate from "~templates/ResponsePageTemplate";

export const RegistrationStatus = () => {
  return (
    <StatusPageTemplate
      status="success"
      title="Congratulation!"
      subtitle="Your account has been created successfully."
      description="Please login to proceed."
      action="Login"
    />
  );
};
