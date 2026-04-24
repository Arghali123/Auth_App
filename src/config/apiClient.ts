import useAuth from "@/auth/store";
import { refreshToken } from "@/services/AuthService";
import axios  from "axios";
import toast from "react-hot-toast";

const apiClient = axios.create({
  baseURL: "http://localhost:8082/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
  timeout: 10000,
});

//every request before
apiClient.interceptors.request.use((config)=>{
  const accessToken=useAuth.getState().accessToken;
  console.log(accessToken)
  if(accessToken)
  {
    config.headers.Authorization=`Bearer ${accessToken}`;
  }
  return config;
})

let isRefreshing=false;
let pending:any[]=[];

function queueRequest(cb:any)
{
  pending.push(cb);
}

function resolveQueue(newToken:string)
{
  pending.forEach((cb)=>cb(newToken));
  pending=[];
}

//reponse interceptors
apiClient.interceptors.response.use(
  (reponse)=>reponse,
  async (error)=>{
    const is401=error.response.status === 401;
    const original=error.config;
    console.log(original);
    if(!is401 || original._retry)
    {
      //message:
      if(error.reponse && error.reponse.data)
      {
        toast.error(error.response.data?.message || "An error occured");
      }
      console.log("API ERROR: ",error.response.data);
      console.log("Full error: ",error);
      return Promise.reject(error);
    }
    original._retry=true;

    //we will try to refresh the token
    if(isRefreshing)
    {
      console.log("addded to queue");
      return new Promise((resolve,reject)=>{

        queueRequest((newToken:string)=>{

          if(!newToken) return reject();
          original.headers.Authorization=`Bearer ${newToken}`;
          resolve(apiClient(original));
        })
      })
    }

    //start refresh
    isRefreshing=true;
    try{
      console.log("start refreshing");
      const loginResponse=await refreshToken();
      const newToken=loginResponse.refreshToken;
      if(!newToken) throw new Error("no access token received");
      useAuth.getState().changeLocalLoginData(
        loginResponse.accessToken,
        loginResponse.user,
        true
      );
      resolveQueue(newToken);
      return apiClient(original);
    }catch(error)
    {
      resolveQueue("null");
      useAuth.getState().logout();
      return Promise.reject(error);
    }finally{
      isRefreshing=false;
    }

  }
)

export default apiClient;