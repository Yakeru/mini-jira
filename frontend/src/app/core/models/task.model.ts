export interface Task {
    id: string;
    title: string;
    description: string | null;
    status: string;
    priority: string;
    projectId: string;
    reporterId: string;
    assigneeId: string | null;
    dueDate: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface TaskRequest {
    title: string;
    description: string | null;
    priority: string;
    assigneeId: string | null;
    dueDate: string | null;
}