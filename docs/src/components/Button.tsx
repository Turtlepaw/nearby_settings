interface LinkButtonProps {
  text: string;
  link: string;
  icon: React.ReactNode; 
  target?: string; 
}

export const LinkButton: React.FC<LinkButtonProps> = ({ text, link, icon, target = "_blank" }) => {

  const openLinkInNewTab = () => {
    window.open(link, target)
  }

  return (
    <div className="flex justify-center my-4 ">
      <button className="border-2 flex gap-2 justify-center items-center p-2 rounded-xl min-w-[200px] link-button" type="button" onClick={openLinkInNewTab} >
        {icon} 
        <b> {text} </b>
      </button>
    </div>
  );
};
