import { nodesConfig } from './NodesConfig';

export const nodeTypes = Object.fromEntries(
  Object.entries(nodesConfig).map(([type, config]) => [type, config.component])
);

export const nodeTypeDetails = Object.fromEntries(
  Object.entries(nodesConfig).map(([type, config]) => [type, config.display])
);