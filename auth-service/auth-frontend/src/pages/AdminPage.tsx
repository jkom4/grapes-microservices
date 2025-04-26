import React, { useEffect, useState } from "react";
import UserList from "../sections/UsersList";
import Loader from "../components/Loader";
import {
    disableUser,
    enableUser,
    getAllUsers,
    logout,
    updateUser
} from "../services/authService";
import { toast } from "react-toastify";
import { Role, User } from "../models/User";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const AdminPage = () => {
    const { isAuthenticated, token, role, id, setToken } = useAuth();
    const navigate = useNavigate();
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const usersPerPage = 10;

    const roles: Role[] = ['USER', 'ADMIN', 'DELIVERY'];

    useEffect(() => {
        if (!token) {
            setLoading(false);
            return;
        }

        const fetchUsers = async () => {
            setLoading(true);
            try {
                const users = await getAllUsers(token);
                setUsers(users);
            } catch (error: unknown) {
                if (error instanceof Error) {
                    toast.error("Failed to load users: " + error.message, { autoClose: 2000 });
                } else {
                    toast.error("An unknown error occurred while fetching users", { autoClose: 2000 });
                }
            } finally {
                setLoading(false);
            }
        };

        fetchUsers();
    }, [token]);

    if (!isAuthenticated || role !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    const handleRoleChange = (userId: string, newRole: Role) => {
        setUsers(prevUsers =>
            prevUsers.map(user =>
                user.id === userId ? { ...user, role: newRole } : user
            )
        );
    };

    const kickCurrentUser = async () => {
        setLoading(true)
        try {
            await logout(token);
            toast.success("You are kicked because something changed on your account", {autoClose: 2000});
        } catch (error: any) {
            toast.error(error.message || "Logout failed.", {autoClose: 2000});
        } finally {
            setLoading(false);
            await setToken(null);
            navigate("/");
        }
    };

    const handleUpdate = async (user: User) => {
        setLoading(true);
        try {
            await updateUser(user, token!);
            if (user.id === id) {
                kickCurrentUser();
                return;
            }
        } catch (error: unknown) {
            if (error instanceof Error) {
                toast.error('An error occurred while updating user: ' + error.message);
            } else {
                toast.error('An unknown error occurred while updating user');
            }
        } finally {
            setLoading(false);
            window.location.reload();
        }
    };

    const handleDisable = async (userId: string | undefined) => {
        setLoading(true);
        try {
            await disableUser(userId, token!);
            toast.success('User disabled successfully');
        } catch (error: unknown) {
            if (error instanceof Error) {
                toast.error('Failed to disable user: ' + error.message);
            } else {
                toast.error('An unknown error occurred while disabling user');
            }
            console.error('Disable user error:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleEnable = async (userId: string | undefined) => {
        setLoading(true);
        try {
            await enableUser(userId, token!);
            toast.success('User enabled successfully');
        } catch (error: unknown) {
            if (error instanceof Error) {
                toast.error('Failed to enable user: ' + error.message);
            } else {
                toast.error('An unknown error occurred while enabling user');
            }
            console.error('Enable user error:', error);
        } finally {
            setLoading(false);
        }
    };

    // Pagination logic
    const indexOfLastUser = currentPage * usersPerPage;
    const indexOfFirstUser = indexOfLastUser - usersPerPage;
    const paginatedUsers = users.slice(indexOfFirstUser, indexOfLastUser);
    const totalPages = Math.ceil(users.length / usersPerPage);

    return (
        <div className="flex flex-col">
            {loading && <Loader />}
            <UserList
                users={paginatedUsers}
                roles={roles}
                handleRoleChange={handleRoleChange}
                handleUpdate={handleUpdate}
                disableUser={handleDisable}
                enableUser={handleEnable}
                token={token}
            />

            <div className="flex justify-center gap-2 my-4">
                <button
                    onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                    disabled={currentPage === 1}
                    className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
                >
                    Previous
                </button>
                <span className="px-4 py-2">{currentPage} / {totalPages}</span>
                <button
                    onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                    disabled={currentPage === totalPages}
                    className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
                >
                    Next
                </button>
            </div>
        </div>
    );
};

export default AdminPage;
