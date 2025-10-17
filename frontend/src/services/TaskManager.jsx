import { Stomp } from '@stomp/stompjs';

export class TaskManager {
    constructor() {
        this.stompClient = null;
        this.webSocket = null;
        this.subscriptions = {};
        this.isConnected = false;
        this.currentTaskId = null;
        this.connectingPromise = null;
        this.taskStatuses = {};
        this.initializeConnection();
    }

    initializeConnection() {
        if (this.connectingPromise) return this.connectingPromise;

        this.connectingPromise = new Promise((resolve, reject) => {
            try {
                console.log('Connecting to WebSocket...');
                const wsUrl = 'ws://localhost:8080/ws/websocket';
                this.webSocket = new WebSocket(wsUrl);
                this.stompClient = Stomp.over(this.webSocket);
                this.stompClient.debug = process.env.NODE_ENV === 'development' ? console.log : () => {};

                this.stompClient.connect({}, frame => {
                    console.log('STOMP Connected:', frame);
                    this.isConnected = true;
                    this.connectingPromise = null;
                    resolve(true);
                }, error => {
                    console.error('STOMP Error:', error);
                    this.isConnected = false;
                    this.connectingPromise = null;
                    reject(error);
                    setTimeout(() => this.initializeConnection(), 5000);
                });

                this.webSocket.onclose = (event) => {
                    console.log('WebSocket connection closed:', event);
                    this.isConnected = false;
                    this.connectingPromise = null;
                    if (event.code !== 1000) {
                        setTimeout(() => this.initializeConnection(), 5000);
                    }
                };

                this.webSocket.onerror = (error) => {
                    console.error('WebSocket error:', error);
                    this.connectingPromise = null;
                };

            } catch (error) {
                console.error('Error setting up WebSocket:', error);
                this.connectingPromise = null;
                reject(error);
                setTimeout(() => this.initializeConnection(), 5000);
            }
        });

        return this.connectingPromise;
    }

    setCurrentTask(taskId) {
        console.log(`Setting current task to: ${taskId}`);
        this.currentTaskId = taskId;
    }

    monitorCurrentTask(sceneId, onProgress, onComplete, onError, initialData = null) {
        if (!this.currentTaskId) {
            const errorMsg = 'No current task set. Call setCurrentTask() first.';
            console.error(errorMsg);
            if (onError) onError(errorMsg);
            return;
        }

        console.log(`Monitoring current task: ${this.currentTaskId}`);
        this.monitorTask(this.currentTaskId, initialData, onProgress, onComplete, onError);
    }

    monitorTask(taskId, initialData, onProgress, onComplete, onError) {
        this.setCurrentTask(taskId);
        console.log(`[${new Date().toISOString()}] User PolinaPolupan monitoring task ${taskId}`);

        this.subscribeToTask(taskId, onProgress, onComplete, onError);

        if (initialData) {
            const status = initialData.status.toUpperCase();
            console.log(`Initial task status: ${status}`);
            this.taskStatuses[taskId] = status.toLowerCase();

            if (status === 'COMPLETED' || status === 'RUNNING' || status === 'PROCESSING') {
                if (onProgress) {
                    const current = initialData.processedNodes || 0;
                    const total = initialData.totalNodes || 1;
                    const percent = Math.round((current / total) * 100);
                    onProgress({ current, total, percent });
                }

                if (status === 'COMPLETED') {
                    setTimeout(() => {
                        if (onComplete) onComplete(initialData);
                    }, 100);
                }
            }
            else if (status === 'FAILED') {
                const errorMsg = initialData.errorMessage || 'Task failed';
                console.log('Task already failed, calling onError with:', errorMsg);
                if (onError) onError(errorMsg);
            }
        }
    }

    subscribeToTask(taskId, onProgress, onComplete, onError) {
        const destination = `/topic/processing/${taskId}`;

        const setupSubscription = () => {
            try {
                console.log(`Subscribing to: ${destination}`);
                this.unsubscribeFromTask(taskId);

                const subscription = this.stompClient.subscribe(destination, (message) => {
                    try {
                        const taskData = JSON.parse(message.body);
                        console.log(`💬 WebSocket message for task ${taskId}:`, taskData);

                        const status = taskData.status?.toLowerCase();

                        this.taskStatuses[taskId] = status;

                        if (status === 'processing' || status === 'running') {
                            if (onProgress) {
                                const current = taskData.processedNodes || 0;
                                const total = taskData.totalNodes || 1;
                                const percent = Math.round((current / total) * 100);
                                console.log(`Progress update: ${current}/${total} (${percent}%)`);
                                onProgress({ current, total, percent });
                            }
                        }
                        else if (status === 'completed') {
                            console.log('************ COMPLETION MESSAGE RECEIVED ************');
                            if (onProgress) {
                                const total = taskData.totalNodes || 1;
                                onProgress({ current: total, total, percent: 100 });
                            }

                            setTimeout(() => {
                                if (onComplete) onComplete(taskData);

                                setTimeout(() => {
                                    this.unsubscribeFromTask(taskId);
                                }, 2000);
                            }, 100);
                        }
                        else if (status === 'failed') {
                            if (onError) onError(taskData.errorMessage || 'Task failed');

                            setTimeout(() => {
                                this.unsubscribeFromTask(taskId);
                            }, 1000);
                        }
                    } catch (error) {
                        console.error(`Error parsing WebSocket message for task ${taskId}:`, error);
                        if (onError) onError('Error processing task update');
                    }
                });

                this.subscriptions[taskId] = subscription;
                console.log(`Successfully subscribed to task ${taskId}`);
            } catch (error) {
                console.error(`Error subscribing to task ${taskId}:`, error);
                if (onError) onError('Failed to subscribe to task updates');
            }
        };

        if (!this.isConnected) {
            console.log('STOMP not connected yet, waiting...');
            this.initializeConnection()
                .then(() => {
                    console.log('Connection established, now subscribing to task');
                    setupSubscription();
                })
                .catch(error => {
                    console.error('Failed to establish connection:', error);
                    if (onError) onError('Failed to connect to WebSocket server');
                });
        } else {
            setupSubscription();
        }
    }

    unsubscribeFromTask(taskId) {
        const subscription = this.subscriptions[taskId];
        if (subscription) {
            subscription.unsubscribe();
            delete this.subscriptions[taskId];
            console.log(`Unsubscribed from task ${taskId}`);
        }
    }

    unsubscribeAll() {
        Object.keys(this.subscriptions).forEach(taskId => {
            this.unsubscribeFromTask(taskId);
        });
    }

    disconnect() {
        this.unsubscribeAll();
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.isConnected = false;
        }
    }
}

export const taskManager = new TaskManager();