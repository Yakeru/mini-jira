export interface Comment {
  id: string;
  content: string;
  taskId: string;
  authorId: string;
  authorUsername: string;
  createdAt: string;
}

export interface CommentRequest {
  content: string;
}
