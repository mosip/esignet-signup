interface TermsAndPrivacyProps {
  title: string
  content: string
}

export const TermsAndPrivacyPage = ({ title, content }: TermsAndPrivacyProps) => {
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
