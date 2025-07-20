
import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';

export default function S3Output({ id, data }) {
    return (
      <div>
        <NodeHeader title={"S3 Output"}/>
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
            handleId="access_key_id"
            handleLabel="Access key"
            type = "text"
            parameterType="STRING"
        />
        <InputHandle
            id={id}
            data={data}
            handleId="secret_access_key"
            handleLabel="Secret key"
            type = "text"
            parameterType="STRING"
        />
        <InputHandle
            id={id}
            data={data}
            handleId="region"
            handleLabel="Region"
            type = "text"
            parameterType="STRING"
        />
        <InputHandle
            id={id}
            data={data}
            handleId="bucket"
            handleLabel="Bucket"
            type = "text"
            parameterType="STRING"
        />
      </div>
    );
}