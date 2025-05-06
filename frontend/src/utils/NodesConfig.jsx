import Floor from '../components/nodes/Floor';
import Input from '../components/nodes/Input';
import Combine from '../components/nodes/Combine';
import Output from '../components/nodes/Output';
import GaussianBlur from '../components/nodes/GaussianBlur';
import S3Input from '../components/nodes/S3Input';
import S3Output from '../components/nodes/S3Output';
import String from '../components/nodes/String';
import { IoFolderOutline, IoSaveOutline, IoReload, IoArrowDown, IoCloudOutline } from 'react-icons/io5';
import Classifier from "../components/nodes/Classifier.jsx";
import OutputFile from "../components/nodes/OutputFile.jsx";
import MedianBlur from "../components/nodes/MedianBlur.jsx";
import Vector2i from "../components/nodes/Vector2D.jsx";
import Vector2D from "../components/nodes/Vector2D.jsx";
import Blur from "../components/nodes/Blur.jsx";
import BoxFilter from "../components/nodes/BoxFilter.jsx";
import BilateralFilter from "../components/nodes/BilateralFilter.jsx";

const InputIcon = ({ theme }) => (
  <div
    style={{
      width: '16px',
      height: '16px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <IoFolderOutline style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
  </div>
);

const OutputIcon = ({ theme }) => (
  <div
    style={{
      width: '16px',
      height: '16px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <IoSaveOutline style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
  </div>
);

const BlurIcon = ({ theme }) => (
  <div
    style={{
      width: '16px',
      height: '16px',
      borderRadius: '50%',
      background: 'radial-gradient(circle, rgba(255,255,255,0.3), rgba(255,255,255,0))',
      filter: 'blur(2px)',
      border: `1px solid ${theme?.colors?.onSurface || '#d8dde2'}`,
    }}
  />
);

const CombineIcon = ({ theme }) => (
  <div
    style={{
      width: '16px',
      height: '16px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <IoReload style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
  </div>
);

const FloorIcon = ({ theme }) => (
  <div
    style={{
      width: '16px',
      height: '16px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <IoArrowDown style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
  </div>
);

const CloudIcon = ({ theme }) => (
  <div
    style={{
      width: '16px',
      height: '16px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <IoCloudOutline style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
  </div>
);

const StringBadge = ({ theme }) => (
  <div
    style={{
      width: '16px',
      height: '16px',
      borderRadius: '4px',
      background: theme?.colors?.primary || '#5a6a7a',
      color: theme?.colors?.onPrimary || '#d8dde2',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontSize: '10px',
      fontWeight: 'bold',
    }}
  >
    S
  </div>
);

export const nodesConfig = {
  Input: {
    component: Input,
    display: {
      description: 'Load images from local storage',
      color: '#7986CB',
      icon: InputIcon
    },
    defaultData: {
      files: []
    },
    handles: {
      files: { source: 'FILENAMES_ARRAY', target: 'FILENAMES_ARRAY' }
    }
  },
  Output: {
    component: Output,
    display: {
      description: 'Save processed images',
      color: '#4DB6AC',
      icon: OutputIcon
    },
    defaultData: {
      files: [],
      prefix: 'output'
    },
    handles: {
      files: { target: 'FILENAMES_ARRAY' },
      folder: { target: 'STRING' }
    }
  },
  GaussianBlur: {
    component: GaussianBlur,
    display: {
      description: 'Apply Gaussian blur filter',
      color: '#FF8A65',
      icon: BlurIcon
    },
    defaultData: {
      files: [],
      sizeX: 3,
      sizeY: 3,
      sigmaX: 1,
      sigmaY: 1
    },
    handles: {
      files: { target: 'FILENAMES_ARRAY', source: 'FILENAMES_ARRAY' },
      sizeX: { target: 'INT' },
      sizeY: { target: 'INT' },
      sigmaX: { target: 'DOUBLE' },
      sigmaY: { target: 'DOUBLE' }
    }
  },
BilateralFilter: {
    component: BilateralFilter,
    display: {
        description: 'Apply bilateral filter',
        color: '#FF8A65',
        icon: BlurIcon
    },
    defaultData: {
        files: [],
        d: 1,
        sigmaColor: 1,
        sigmaSpace: 1
    },
    handles: {
        files: { target: 'FILENAMES_ARRAY', source: 'FILENAMES_ARRAY' },
        d: { target: 'INT' },
        sigmaColor: { target: 'DOUBLE' },
        sigmaSpace: { target: 'DOUBLE' }
    }
},
MedianBlur: {
    component: MedianBlur,
    display: {
        description: 'Apply median blur filter',
        color: '#FF8A65',
        icon: BlurIcon
    },
    defaultData: {
        files: [],
        ksize: 3
    },
    handles: {
        files: { target: 'FILENAMES_ARRAY', source: 'FILENAMES_ARRAY' },
        ksize: { target: 'INT' }
    }
},
Blur: {
    component: Blur,
    display: {
        description: 'Apply blur filter',
        color: '#FF8A65',
        icon: BlurIcon
    },
    defaultData: {
        files: [],
        ksize: null
    },
    handles: {
        files: { target: 'FILENAMES_ARRAY', source: 'FILENAMES_ARRAY' },
        ksize: { target: 'VECTOR2D' }
    }
},
BoxFilter: {
    component: BoxFilter,
    display: {
        description: 'Apply box filter',
        color: '#FF8A65',
        icon: BlurIcon
    },
    defaultData: {
        files: [],
        ddepth: -1,
        ksize: null
    },
    handles: {
        files: { target: 'FILENAMES_ARRAY', source: 'FILENAMES_ARRAY' },
        ddepth: { target: 'INT' },
        ksize: { target: 'VECTOR2D' }
    }
},
Vector2D: {
    component: Vector2D,
    display: {
        description: 'Vector2D',
        color: '#FF8A65',
        icon: BlurIcon
    },
    defaultData: {
        x: 0,
        y: 0
    },
    handles: {
        x: { target: 'DOUBLE'},
        y: { target: 'DOUBLE'},
        vector2D: { source: 'VECTOR2D' }
    }
  },
  Combine: {
    component: Combine,
    display: {
      description: 'Merge multiple images',
      color: '#AED581',
      icon: CombineIcon
    },
    defaultData: {
      files_0: [],
      files_1: [],
      files_2: [],
      files_3: [],
      files_4: [],
      files_5: []
    },
    handles: {
      files_0: { target: 'FILENAMES_ARRAY' },
      files_1: { target: 'FILENAMES_ARRAY' },
      files_2: { target: 'FILENAMES_ARRAY' },
      files_3: { target: 'FILENAMES_ARRAY' },
      files_4: { target: 'FILENAMES_ARRAY' },
      files: { source: 'FILENAMES_ARRAY' }
    }
  },
  Floor: {
    component: Floor,
    display: {
      description: 'Round down values',
      color: '#BA68C8',
      icon: FloorIcon
    },
    defaultData: {
      number: 0
    },
    handles: {
      number: { source: 'DOUBLE', target: 'DOUBLE' }
    }
  },
  S3Input: {
    component: S3Input,
    display: {
      description: 'Load images from AWS S3',
      color: '#4FC3F7',
      icon: CloudIcon
    },
    defaultData: {
      access_key_id: "",
      secret_access_key: "",
      region: "",
      bucket: ""
    },
    handles: {
      files: { source: 'FILENAMES_ARRAY' },
      access_key_id: { target: 'STRING' },
      secret_access_key: { target: 'STRING' },
      region: { target: 'STRING' },
      bucket: { target: 'STRING' }
    }
  },
  S3Output: {
    component: S3Output,
    display: {
      description: 'Save images to AWS S3',
      color: '#81C784',
      icon: CloudIcon
    },
    defaultData: {
      files: [],
      access_key_id: "",
      secret_access_key: "",
      region: "",
      bucket: ""
    },
    handles: {
      files: { target: 'FILENAMES_ARRAY' },
      access_key_id: { target: 'STRING' },
      secret_access_key: { target: 'STRING' },
      region: { target: 'STRING' },
      bucket: { target: 'STRING' }
    }
  },
  String: {
    component: String,
    display: {
      description: 'String value',
      color: '#81C784',
      icon: StringBadge
    },
    defaultData: {
      value: ""
    },
    handles: {
      value: { target: 'STRING', source: 'STRING' }
    }
  },
  Classifier: {
    component: Classifier,
    display: {
      description: 'Classifier',
      color: '#81C784',
      icon: StringBadge
    },
    defaultData: {
        files: []
    },
    handles: {
        files: { target: 'FILENAMES_ARRAY' },
        json: { source: 'STRING' }
    }
  },
  OutputFile: {
    component: OutputFile,
    display: {
        description: 'OutputFile',
        color: '#81C784',
        icon: StringBadge
    },
    defaultData: {
        filename: "new.txt",
        content: ""
    },
    handles: {
        filename: { target: 'STRING' },
        content: { target: 'STRING' }
    }
  }
};