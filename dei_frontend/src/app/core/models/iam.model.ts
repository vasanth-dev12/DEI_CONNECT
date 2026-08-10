import { DepartmentName, Role, UserStatus } from './enums';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  employeeId: string;
  name: string;
  email: string;
  role: Role;
}

export interface AdminCreateUserRequest {
  employeeId: string;
  name: string;
  email: string;
  password: string;
  role: Role;
  departmentName: DepartmentName;
  gradeId?: number | null;
  status?: UserStatus | null;
  managerId?: number | null;
  hrId?: number | null;
  salary: number;
  yearsOfExperience: number;
}

export interface AdminUpdateUserRequest {
  name: string;
  email: string;
  role: Role;
  status: UserStatus;
  departmentName: DepartmentName;
  gradeId?: number | null;
  managerId?: number | null;
  hrId?: number | null;
  salary: number;
  yearsOfExperience: number;
}

export interface UpdateProfileRequest {
  name: string;
  email: string;
  password?: string | null;
}

export interface UserResponse {
  userId: number;
  employeeId: string;
  name: string;
  email: string;
  role: Role;
  departmentId: number | null;
  departmentName: DepartmentName | null;
  gradeId: number | null;
  status: UserStatus;
  managerId: number | null;
  managerName: string | null;
  hrId: number | null;
  hrName: string | null;
  createdDate: string;
  lastModifiedDate: string;
  salary: number | null;
  yearsOfExperience: number | null;
}

export interface ScopeValueOption {
  value: string;
  label: string;
}

export interface AuditLogResponse {
  auditId: number;
  userId: number;
  action: string;
  entityType: string;
  recordId: number;
  timestamp: string;
}

export interface CurrentUser {
  userId: number;
  employeeId: string;
  name: string;
  email: string;
  role: Role;
}
