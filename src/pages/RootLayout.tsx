import Navbar from '@/components/Navbar'
import { Toaster } from "react-hot-toast";

import { Outlet } from 'react-router'

function RootLayout() {
  return (
    <div>
      <Toaster/>
      <Navbar/>
      <Outlet/>
    </div>
  )
}

export default RootLayout
