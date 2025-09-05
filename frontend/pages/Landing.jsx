import React, { useEffect, useRef, useState } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
gsap.registerPlugin(ScrollTrigger);
const TOTAL_FRAMES = 703;
const ANIMATION_END_PERCENTAGE = 100;
const Landing = () => {
    const canvasRef = useRef(null);
    const [images, setImages] = useState([]);
    const [loadedCount, setLoadedCount] = useState(0);
    useEffect(() => {
        console.log('Loading images...');
        const frameImages = [];
        let loaded = 0;

        const handleImageLoad = () => {
            loaded++;
            setLoadedCount(loaded);
            console.log(`Loaded ${loaded}/${TOTAL_FRAMES} images`);
        };

        const handleImageError = (index) => {
            console.error(`Failed to load image at index ${index}: /frames/frame_${index.toString().padStart(4,'0')}.jpg`);
        };

        for (let i = 0; i < TOTAL_FRAMES; i++) {
            const img = new Image();
            img.onload = handleImageLoad;
            img.onerror = () => handleImageError(i + 1);
            img.src = `/frames/frame_${(i + 1).toString().padStart(4,'0')}.jpg`;
            frameImages.push(img);
        }

        setImages(frameImages);
    }, []);

    useEffect(() => {
        console.log('Images array length:', images.length);
        if (images.length === 0) return;

        const canvas = canvasRef.current;
        if (!canvas) {
            console.error('Canvas ref is null');
            return;
        }

        const context = canvas.getContext('2d');
        console.log('Canvas context:', context);

        // Set canvas size
        const scale = window.devicePixelRatio || 1;
        canvas.width = 1920 * scale;
        canvas.height = 1080 * scale;
        canvas.style.width = '100vw';
        canvas.style.height = '100vh';
        context.scale(scale, scale);

        const frameState = { frame: 0 };

        const render = () => {
            const img = images[Math.floor(frameState.frame)];

            if (img?.complete) {
                context.clearRect(0, 0, canvas.width / scale, canvas.height / scale);
                context.drawImage(
                    img,
                    0,
                    0,
                    canvas.width / scale,
                    canvas.height / scale
                );
            }
        };

        const startAnimation = () => {
            console.log('Starting GSAP animation');
            gsap.to(frameState, {
                frame: TOTAL_FRAMES - 1,
                snap: "frame",
                ease: 'none',
                scrollTrigger: {
                    trigger: document.body,
                    start: "top top",
                    end: `${ANIMATION_END_PERCENTAGE}%`,
                    scrub: true,
                    markers: true
                },
                onUpdate: render,
                onComplete: () => {
                    console.log('Animation completed - frames will stay static now');
                    const lastImg = images[TOTAL_FRAMES - 1];
                    if (lastImg?.complete) {
                        context.clearRect(0, 0, canvas.width / scale, canvas.height / scale);
                        context.drawImage(
                            lastImg,
                            0,
                            0,
                            canvas.width / scale,
                            canvas.height / scale
                        );
                    }
                }
            });
        };

        if (images[0]?.complete) {
            render();
            startAnimation();
        } else {
            images[0].onload = () => {
                render();
                startAnimation();
            };
        }

        return () => {
            ScrollTrigger.getAll().forEach(trigger => trigger.kill());
        };

    }, [images]);

    return (
        <div style={{ position: 'relative', height: '2500vh' }}>
            <canvas
                ref={canvasRef}
                style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    width: '100vw',
                    height: '100vh',
                    zIndex: 1
                }}
            />

            {/* Content sections that appear as user scrolls */}
            <div style={{ position: 'relative', zIndex: 2 }}>
                {/* Initial spacer */}
                <div style={{ height: '200vh' }}></div>

                <div style={{
                    height: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'rgba(255,255,255,0.1)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <h2 style={{ fontSize: '3rem', color: 'white', textAlign: 'center' }}>
                        DNA Sequence Analysis
                    </h2>
                </div>

                {/* Large spacer during animation phase */}
                <div style={{ height: '500vh' }}></div>

                <div style={{
                    height: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'rgba(0,0,0,0.3)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <h2 style={{ fontSize: '3rem', color: 'white', textAlign: 'center' }}>
                        Animation Complete - Static Phase
                    </h2>
                </div>

                <div style={{ height: '400vh' }}></div>
                <div style={{
                    height: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'rgba(255,255,255,0.2)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <h2 style={{ fontSize: '3rem', color: 'white', textAlign: 'center' }}>
                        Detailed Analysis
                    </h2>
                </div>

                <div style={{ height: '400vh' }}></div>

                <div style={{
                    height: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'rgba(0,255,0,0.1)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <h2 style={{ fontSize: '3rem', color: 'white', textAlign: 'center' }}>
                        Results & Conclusions
                    </h2>
                </div>

                <div style={{ height: '400vh' }}></div>

                <div style={{
                    height: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'rgba(255,0,255,0.1)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <h2 style={{ fontSize: '3rem', color: 'white', textAlign: 'center' }}>
                        Final Section
                    </h2>
                </div>

                {/* Bottom spacer */}
                <div style={{ height: '300vh' }}></div>
            </div>
        </div>
    );
};

export default Landing;