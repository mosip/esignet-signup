import { useRef, useEffect } from "react";
import {
  AlertDialog,
  AlertDialogContent,
} from "~components/ui/alert-dialog";
import { ReactComponent as CloseIconSvg } from "~assets/svg/cross-icon.svg";

interface TermsAndPrivacyModalProps {
  title: string
  content: string
  isOpen: boolean
  backdrop: string
  toggleModal: () => void
}
  
export const TermsAndPrivacyModal = ({ title, content, isOpen, backdrop, toggleModal }: TermsAndPrivacyModalProps) => {
  const modalRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: { target: any; }) => {
      if (backdrop === "static") return;

      if (isOpen && modalRef.current && !modalRef.current.contains(event.target)) {
        toggleModal();
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [modalRef, isOpen]);

  return (
    <AlertDialog open={!!isOpen}>
      <AlertDialogContent ref={modalRef} className="container max-w-[90%] md:w-[1088px] w-[1088px] p-0 bg-white rounded-lg">
        <div className="flex items-center justify-between p-4 md:p-5 border-b rounded-t dark:border-gray-600">
          <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
            {title}
          </h3>
          <button type="button" onClick={toggleModal} className="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ms-auto inline-flex justify-center items-center dark:hover:bg-gray-600 dark:hover:text-white" data-modal-hide="static-modal">
            <CloseIconSvg/>
          </button>
        </div>
        <div className="p-4 md:p-5 space-y-4">
          <div className="" dangerouslySetInnerHTML={{__html: content}}></div>
        </div>
      </AlertDialogContent>
    </AlertDialog>
  )
};
