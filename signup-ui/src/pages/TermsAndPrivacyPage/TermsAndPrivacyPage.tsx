interface TermsAndPrivacyProps {
  title: string
  content: string
}

export const TermsAndPrivacyPage = ({ title, content }: TermsAndPrivacyProps) => {
  // const title = "Terms & Conditions";
  // const content = `<div class=""><div><p>At ភាសាខ្មែរ(KhmerLang), accessible from khmerlang.com, one of our main priorities is the privacy of our visitors. This Privacy Policy document contains types of information that is collected and recorded by Khmerlang and how we use it.</p><p>If you have additional questions or require more information about our Privacy Policy, do not hesitate to contact us.</p><p>This Privacy Policy applies only to our online activities and is valid for visitors to our website with regards to the information that they shared and/or collect in Khmerlang. This policy is not applicable to any information collected offline or via channels other than this website.</p><h2>Consent</h2><p>By using our website, you hereby consent to our Privacy Policy and agree to its terms.</p><h2 style="font-weight:bold">Information we collect</h2><p>The personal information that you are asked to provide, and the reasons why you are asked to provide it, will be made clear to you at the point we ask you to provide your personal information.</p><p>If you contact us directly, we may receive additional information about you such as your name, email address, phone number, the contents of the message and/or attachments you may send us, and any other information you may choose to provide.</p><p>When you register for an Account, we may ask for your contact information, including items such as name, company name, address, email address, and telephone number.</p><h2>How we use your information</h2><p>We use the information we collect in various ways, including to:</p><ul><li>Provide, operate, and maintain our website</li><li>Improve, personalize, and expand our website</li><li>Understand and analyze how you use our website</li><li>Develop new products, services, features, and functionality</li><li>Communicate with you, either directly or through one of our partners, including for customer service, to provide you with updates and other information relating to the website, and for marketing and promotional purposes</li><li>Send you emails</li><li>Find and prevent fraud</li></ul><h2>Our Advertising Partners</h2><p>Some of advertisers on our site may use cookies and web beacons. Our advertising partners are listed below. Each of our advertising partners has their own Privacy Policy for their policies on user data.</p></div></div>`;
    return (
      <div>
        <div className="container w-[1088px] p-5 py-10 ps-10">
          <h3 className="text-2xl font-bold">{title}</h3>
        </div>
        <div className="container w-[1088px] border-[1px] rounded-lg bg-white p-10 py-15 mb-10">
          <div className="" dangerouslySetInnerHTML={{__html: content}}>
          </div>
        </div>
      </div>
    )
  };
  