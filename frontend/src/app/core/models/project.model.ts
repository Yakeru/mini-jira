export interface Project {
    id: string;
    name: string;
    description: string | null;
    status: string;
    ownerId: string;
    createdAt: string;
    updatedAt: string;
}

export interface ProjectRequest {
    name: string;
    description: string | null;
}