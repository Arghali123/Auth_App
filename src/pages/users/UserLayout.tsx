import useAuth from "@/auth/store";
import { Navigate, Outlet } from "react-router";

function UserLayout() {
  const checkLogin = useAuth((state) => state.checkLogin);

  if (!checkLogin()) {
    return <Navigate to="/login" />;
  }

  return (
    <div>
      <Outlet />
    </div>
  );
}

export default UserLayout;
