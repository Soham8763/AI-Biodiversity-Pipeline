import { useEffect, useState } from 'react'
import Lenis from 'lenis'
import viteLogo from '/vite.svg'
import './App.css'
import Landing from '../pages/Landing'
import UploadComponent from '../pages/UploadComponent'

function App() {

  useEffect(()=>{
    const lenis = new Lenis();
    function raf(time) {
    lenis.raf(time);
    requestAnimationFrame(raf);
    }
    requestAnimationFrame(raf);
  })

  return (
    <>
      <UploadComponent/>
    </>
  )
}

export default App
