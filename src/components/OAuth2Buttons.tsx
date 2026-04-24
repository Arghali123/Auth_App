import { Button } from "./ui/button";
import { Globe, Shield } from "lucide-react";

function OAuth2Buttons() {
  const oauthBaseUrl = (
    import.meta.env.VITE_BASE_URL || "http://localhost:8082"
  ).replace(/\/$/, "");

  return (
    <div className="space-y-3">
      <Button
        asChild
        variant="outline"
        className="w-full items-center gap-3 rounded-2xl"
      >
        <a href={`${oauthBaseUrl}/oauth2/authorization/google`}>
          <Globe />
          Continue with Google
        </a>
      </Button>

      <Button
        asChild
        variant="outline"
        className="w-full items-center gap-3 rounded-2xl"
      >
        <a href={`${oauthBaseUrl}/oauth2/authorization/github`}>
          <Shield />
          Continue with Github
        </a>
      </Button>
    </div>
  );
}

export default OAuth2Buttons;
