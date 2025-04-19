import React, {useState} from 'react';
import { toast } from 'react-toastify';
import {Role, User} from "../models/User";

interface UserListProps {
    users: User[];
    roles: Role[];
    handleRoleChange: (userId: string, role: Role) => void;
    handleUpdate: (user: User) => void;
    disableUser: (userId: string, token: string) => Promise<void>;
    enableUser: (userId: string, token: string) => Promise<void>;
    token: string;
}

const UserList: React.FC<UserListProps> = ({
                                               users,
                                               roles,
                                               handleRoleChange,
                                               handleUpdate,
                                               disableUser,
                                               enableUser,
                                               token,
                                           }) => {

    const [searchTerm, setSearchTerm] = useState('');

    const filteredUsers = users.filter(user => {
        const lowerSearchTerm = searchTerm.toLowerCase();
        return (
            user.email.toLowerCase().includes(lowerSearchTerm) ||
            user.firstName.toLowerCase().includes(lowerSearchTerm) ||
            user.name.toLowerCase().includes(lowerSearchTerm) ||
            user.id!.toString().includes(lowerSearchTerm)
        );
    });

    return (
        <div className="flex-1 py-4 sm:px-4 md:px-6">
            <h1 className="text-3xl font-bold mb-6 p-12 pb-0">User List</h1>

            <div className="mb-4">
                <input
                    type="text"
                    placeholder="Search by email, name, or ID..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="p-2 border border-gray-300 rounded w-full"
                />
            </div>

            <div>
                <table className="min-w-full bg-white shadow-md rounded-lg overflow-hidden p-4">
                    <thead className="bg-gray-200 text-gray-700">
                    <tr>
                        <th className="px-4 py-3 text-left text-sm w-16">ID</th>
                        <th className="px-4 py-3 text-left text-sm w-32">Name</th>
                        <th className="px-4 py-3 text-left text-sm w-40">Email</th>
                        <th className="px-4 py-3 text-left text-sm w-32">Phone</th>
                        <th className="px-4 py-3 text-left text-sm w-32">National ID</th>
                        <th className="px-4 py-3 text-left text-sm w-24">Gender</th>
                        <th className="px-4 py-3 text-left text-sm w-32">Role</th>
                        <th className="px-4 py-3 text-left text-sm w-32">Profession</th>
                        <th className="px-4 py-3 text-left text-sm w-20">Age</th>
                        <th className="px-4 py-3 text-left text-sm w-20">Active</th>
                        <th className="px-4 py-3 text-left text-sm w-20">Update</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredUsers.length > 0 ? (
                        filteredUsers.map((user) => (
                            <tr key={user.id} className="border-b hover:bg-gray-50">
                                <td className="px-4 py-4 text-sm">{user.id}</td>
                                <td className="px-4 py-4 text-sm">{user.firstName} {user.name}</td>
                                <td className="px-4 py-4 text-sm">{user.email} {user.emailVerified ? '(verified)' : ''}</td>
                                <td className="px-4 py-4 text-sm">{user.phoneNumber} {user.phoneVerified ? '(verified)' : ''}</td>
                                <td className="px-4 py-4 text-sm">{user.nationalId}</td>
                                <td className="px-4 py-4 text-sm">{user.gender}</td>
                                <td className="px-4 py-4 text-sm">
                                    <select
                                        value={user.role}
                                        onChange={(e) => {
                                            const role = e.target.value;
                                            if (role && ['USER', 'ADMIN', 'DELIVERY'].includes(role)) {
                                                handleRoleChange(user.id!, role as Role);
                                            }
                                        }}
                                        className="p-2 border border-gray-300 rounded"
                                    >
                                        {roles.map(role => (
                                            <option key={role} value={role}>
                                                {role}
                                            </option>
                                        ))}
                                    </select>
                                </td>
                                <td className="px-4 py-4 text-sm">{user.profession}</td>
                                <td className="px-4 py-4 text-sm">{user.age}</td>
                                <td className="px-4 py-4 text-sm">
                                    <button
                                        onClick={async () => {
                                            const action = user.active ? 'disable' : 'enable';
                                            const confirmMsg = `Are you sure you want to ${action} this user (${user.firstName} ${user.name})?`;
                                            if (window.confirm(confirmMsg)) {
                                                try {
                                                    if (user.active) {
                                                        await disableUser(user.id!, token);
                                                        toast.success('User disabled successfully!');
                                                        window.location.reload();
                                                    } else {
                                                        await enableUser(user.id!, token);
                                                        toast.success('User enabled successfully!');
                                                        window.location.reload();
                                                    }
                                                } catch (error) {
                                                    toast.error('Failed to update user');
                                                }
                                            }
                                        }}
                                        className={`px-3 py-1 rounded text-white text-xs ${
                                            user.active ? 'bg-red-500 hover:bg-red-600' : 'bg-green-500 hover:bg-green-600'
                                        }`}
                                    >
                                        {user.active ? 'Disable' : 'Enable'}
                                    </button>
                                </td>
                                <td className="px-4 py-4 text-sm">
                                    <button
                                        onClick={() => handleUpdate(user)}
                                        className="px-4 py-2 bg-blue-500 text-white rounded"
                                    >
                                        Update
                                    </button>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={12} className="px-4 py-4 text-center text-gray-500">
                                No users found.
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default UserList;
