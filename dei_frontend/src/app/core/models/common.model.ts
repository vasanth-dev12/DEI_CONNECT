export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface PageQuery {
  page?: number;
  size?: number;
  sort?: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: ApiFieldError[];
}

export interface ApiFieldError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}
