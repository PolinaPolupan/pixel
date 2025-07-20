import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';

export default function Output({ id, data }) {
return (
  <div>
    <NodeHeader title={"Output"}/>
    <LabeledHandle
        label="Files"
        type="target"
        position="left"
        id="files"
        parameterType="FILENAMES_ARRAY"
    />
    <InputHandle
        id={id}
        data={data}
        handleId="prefix"
        handleLabel="Prefix"
        type = "text"
        parameterType="STRING"
    />
    <InputHandle
        id={id}
        data={data}
        handleId="folder"
        handleLabel="Folder"
        type = "text"
        parameterType="STRING"
    />
  </div>
);
}