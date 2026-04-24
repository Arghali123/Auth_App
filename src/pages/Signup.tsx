import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { motion } from "framer-motion";
import { Mail, Lock, User } from "lucide-react";
import type RegisterData from "@/models/RegisterData";
import React, { useState } from "react";
import { useNavigate } from "react-router";
import { toast } from "react-hot-toast";
import axios from "axios";
import { registerUser } from "@/services/AuthService";
import OAuth2Buttons from "@/components/OAuth2Buttons";

function Signup() {
  const [data, setData] = useState<RegisterData>({
    name: "",
    email: "",
    password: "",
  });
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const navigate = useNavigate();

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setData((value) => ({
      ...value,
      [event.target.name]: event.target.value,
    }));
  };

  const handleFormSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();
    setErrorMessage(null);

    if (data.name.trim() === "") {
      toast.error("Name is required !");
      return;
    }

    if (data.email.trim() === "") {
      toast.error("Email is required !");
      return;
    }

    if (data.password.trim() === "") {
      toast.error("Password is required !");
      return;
    }

    setLoading(true);

    try {
      await registerUser(data);
      toast.success("User register successfully...");
      setData({
        name: "",
        email: "",
        password: "",
      });
      navigate("/login");
    } catch (error) {
      const message = axios.isAxiosError<{ message?: string }>(error)
        ? error.response?.data?.message ?? error.message
        : "Error in registering the user...";

      setErrorMessage(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background text-foreground px-4 py-10">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8 }}
        className="w-full max-w-md"
      >
        <Card className="bg-card/70 backdrop-blur-xl border-border shadow-2xl rounded-2xl p-6">
          <CardContent>
            <motion.h1
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.2 }}
              className="text-4xl font-bold text-center"
            >
              Create Your Account
            </motion.h1>

            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.4 }}
              className="mt-2 text-center text-muted-foreground"
            >
              Join the next-generation authentication platform
            </motion.p>

            <form className="mt-8 space-y-6" onSubmit={handleFormSubmit}>
              <div className="space-y-2">
                <Label htmlFor="name">Name</Label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="name"
                    type="text"
                    placeholder="John Doe"
                    className="pl-10"
                    name="name"
                    value={data.name}
                    onChange={handleInputChange}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="email"
                    type="email"
                    placeholder="you@example.com"
                    className="pl-10"
                    name="email"
                    value={data.email}
                    onChange={handleInputChange}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="password"
                    type="password"
                    placeholder="********"
                    className="pl-10"
                    name="password"
                    value={data.password}
                    onChange={handleInputChange}
                  />
                </div>
              </div>

              {errorMessage ? (
                <p className="text-sm text-red-500">{errorMessage}</p>
              ) : null}

              <Button
                type="submit"
                disabled={loading}
                className="w-full rounded-2xl text-lg"
              >
                {loading ? "Registering..." : "Sign Up"}
              </Button>

              <div className="my-4 flex items-center gap-4">
                <div className="h-[1px] flex-1 bg-border"></div>
                <span className="text-sm text-muted-foreground">OR</span>
                <div className="h-[1px] flex-1 bg-border"></div>
              </div>

            <OAuth2Buttons/>
            </form>
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
}

export default Signup;
