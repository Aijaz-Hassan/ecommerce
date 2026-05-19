export function isAdminRole(role) {
  const normalizedRole = String(role || "").trim().toUpperCase();
  return normalizedRole === "ROLE_ADMIN" || normalizedRole === "ADMIN";
}
