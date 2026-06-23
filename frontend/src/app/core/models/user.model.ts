export interface User {
    id: string;
    username: string;
    name: string | null;
    email: string | null;
    avatarUrl: string | null;
    createdAt: string;
}