export const API = {
  auth: {
    login: '/api/auth/login',
  },
  users: {
    base: '/api/users',
    me: '/api/users/me',
    scopeValues: '/api/users/scope-values',
    byId: (id: number | string) => `/api/users/${id}`,
  },
  auditLogs: {
    base: '/api/audit-logs',
  },
  demographicProfiles: {
    base: '/api/demographic-profiles',
    me: '/api/demographic-profiles/me',
  },
  snapshots: {
    base: '/api/representation-snapshots',
    runs: '/api/representation-snapshots/runs',
    generate: '/api/representation-snapshots/generate',
    byId: (id: number | string) => `/api/representation-snapshots/${id}`,
    distribution: (id: number | string) => `/api/representation-snapshots/${id}/distribution`,
    publish: (id: number | string) => `/api/representation-snapshots/${id}/publish`,
    publishRun: (id: number | string) => `/api/representation-snapshots/runs/${id}/publish`,
    deleteRun: (id: number | string) => `/api/representation-snapshots/runs/${id}`,
  },
  goals: {
    base: '/api/goals',
    byId: (id: number | string) => `/api/goals/${id}`,
    progress: (goalId: number | string) => `/api/goals/${goalId}/progress`,
    progressById: (goalId: number | string, progressId: number | string) =>
      `/api/goals/${goalId}/progress/${progressId}`,
    confirmProgress: (goalId: number | string, progressId: number | string) =>
      `/api/goals/${goalId}/progress/${progressId}/confirm`,
  },
  payEquity: {
    analyses: '/api/pay-equity/analyses',
    analysisById: (id: number | string) => `/api/pay-equity/analyses/${id}`,
    publishAnalysis: (id: number | string) => `/api/pay-equity/analyses/${id}/publish`,
    computeAnalysis: (id: number | string) => `/api/pay-equity/analyses/${id}/compute`,
    flags: (analysisId: number | string) => `/api/pay-equity/analyses/${analysisId}/flags`,
    flagById: (analysisId: number | string, flagId: number | string) =>
      `/api/pay-equity/analyses/${analysisId}/flags/${flagId}`,
    publishedAnalyses: '/api/pay-equity/published/analyses',
    publishedAnalysisById: (id: number | string) => `/api/pay-equity/published/analyses/${id}`,
    publishedFlags: (analysisId: number | string) =>
      `/api/pay-equity/published/analyses/${analysisId}/flags`,
  },
  reports: {
    base: '/api/reports',
    byId: (id: number | string) => `/api/reports/${id}`,
    publish: (id: number | string) => `/api/reports/${id}/publish`,
    data: (id: number | string) => `/api/reports/${id}/data`,
  },
  notifications: {
    base: '/api/notifications',
    unreadCount: '/api/notifications/unread-count',
    byId: (id: number | string) => `/api/notifications/${id}`,
    read: (id: number | string) => `/api/notifications/${id}/read`,
    dismiss: (id: number | string) => `/api/notifications/${id}/dismiss`,
    readAll: '/api/notifications/read-all',
    emit: '/api/notifications/emit',
  },
  surveys: {
    base: '/api/surveys',
    byId: (id: number | string) => `/api/surveys/${id}`,
    launch: (id: number | string) => `/api/surveys/${id}/launch`,
    close: (id: number | string) => `/api/surveys/${id}/close`,
    publish: (id: number | string) => `/api/surveys/${id}/publish`,
    questions: (surveyId: number | string) => `/api/surveys/${surveyId}/questions`,
    questionById: (surveyId: number | string, questionId: number | string) =>
      `/api/surveys/${surveyId}/questions/${questionId}`,
    responses: (surveyId: number | string) => `/api/surveys/${surveyId}/responses`,
    summaries: (surveyId: number | string) => `/api/surveys/${surveyId}/summaries`,
    publishSummary: (surveyId: number | string, summaryId: number | string) =>
      `/api/surveys/${surveyId}/summaries/${summaryId}/publish`,
  },
  ergs: {
    base: '/api/ergs',
    byId: (id: number | string) => `/api/ergs/${id}`,
    memberships: (ergId: number | string) => `/api/ergs/${ergId}/memberships`,
    myMembership: (ergId: number | string) => `/api/ergs/${ergId}/memberships/me`,
    membershipById: (ergId: number | string, membershipId: number | string) =>
      `/api/ergs/${ergId}/memberships/${membershipId}`,
    events: (ergId: number | string) => `/api/ergs/${ergId}/events`,
    eventById: (ergId: number | string, eventId: number | string) =>
      `/api/ergs/${ergId}/events/${eventId}`,
    participate: (ergId: number | string, eventId: number | string) =>
      `/api/ergs/${ergId}/events/${eventId}/participate`,
    participants: (ergId: number | string, eventId: number | string) =>
      `/api/ergs/${ergId}/events/${eventId}/participants`,
  },
} as const;
