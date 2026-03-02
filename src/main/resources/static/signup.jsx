import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Signup() {

  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    phone: "",
    password: "",
    dob: "",
    gender: "",
    name: ""
  });

  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [error, setError] = useState("");
  const [showOtp, setShowOtp] = useState(false);
  const [otp, setOtp] = useState("");
  const [timer, setTimer] = useState(0);
  const [showSuccess, setShowSuccess] = useState(false);

  // OTP Timer
  useEffect(() => {
    let interval;
    if (timer > 0) {
      interval = setInterval(() => {
        setTimer(prev => prev - 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [timer]);

  const calculateAge = (dob) => {
    const birth = new Date(dob);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    return age;
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Request OTP
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!acceptedTerms) {
      setError("Please accept Terms & Privacy Policy.");
      return;
    }

    try {
      await axios.post("http://localhost:8081/api/auth/register/request-otp", {
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        gender: formData.gender,
        age: calculateAge(formData.dob)
      });

      setShowOtp(true);
      setTimer(30);

    } catch {
      setError("Failed to send OTP.");
    }
  };

  // Verify OTP
  const verifyOtp = async () => {
    try {
      await axios.post("http://localhost:8081/api/auth/register/verify-otp", {
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        gender: formData.gender,
        age: calculateAge(formData.dob),
        password: formData.password,
        confirmPassword: formData.password,
        otp: otp,
        acceptedTerms: true
      });

      setShowSuccess(true);

    } catch {
      setError("Invalid OTP.");
    }
  };

  const resendOtp = async () => {
    await handleSubmit(new Event("submit"));
  };

  return (
    <>
      <style>{`
        :root {
          --primary-green: #00704a;
          --bg-cream: #f9f6f2;
          --text-dark: #1a1a1a;
          --text-muted: #555;
          --border-color: #dcdcdc;
        }

        body {
          margin: 0;
          font-family: 'Poppins', sans-serif;
        }

        .signup-container {
          display: flex;
          height: 100vh;
          background-color: var(--bg-cream);
        }

        .hero-section {
          flex: 1.2;
          padding: 0 8%;
          display: flex;
          flex-direction: column;
          justify-content: center;
          background: linear-gradient(rgba(249,246,242,0.8), rgba(249,246,242,0.8)),
          url('https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&q=80&w=2070');
          background-size: cover;
          background-position: center;
        }

        .hero-section h1 {
          font-size: 3.5rem;
          color: var(--primary-green);
          margin-bottom: 20px;
        }

        .hero-section p {
          color: var(--text-muted);
          margin-bottom: 30px;
        }

        .features {
          list-style: none;
          padding: 0;
        }

        .features li {
          color: var(--primary-green);
          font-weight: 600;
          margin-bottom: 12px;
        }

        .form-section {
          flex: 0.8;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 40px;
        }

        .form-card {
          background: white;
          width: 100%;
          max-width: 480px;
          padding: 40px;
          border-radius: 20px;
          box-shadow: 0 15px 35px rgba(0,0,0,0.05);
        }

        .form-header h2 {
          color: var(--primary-green);
          text-align: center;
          margin-bottom: 25px;
        }

        .input-group {
          margin-bottom: 18px;
        }

        .input-group label {
          display: block;
          font-size: 0.85rem;
          font-weight: 600;
          margin-bottom: 8px;
        }

        .input-group input,
        .input-group select {
          width: 100%;
          padding: 12px;
          border: 1px solid var(--border-color);
          border-radius: 10px;
        }

        .terms-container {
          display: flex;
          align-items: center;
          gap: 12px;
          margin: 20px 0;
        }

        .btn-create {
          width: 100%;
          background-color: var(--primary-green);
          color: white;
          padding: 16px;
          border: none;
          border-radius: 10px;
          font-weight: 600;
          cursor: pointer;
        }

        .btn-create:hover {
          background-color: #005a3b;
        }

        .error-msg {
          color: red;
          margin-bottom: 15px;
          text-align: center;
        }

        .otp-timer {
          text-align: center;
          margin-bottom: 15px;
          color: var(--primary-green);
        }

        .success-overlay {
          position: fixed;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: rgba(0,0,0,0.5);
          display: flex;
          justify-content: center;
          align-items: center;
        }

        .success-box {
          background: white;
          padding: 30px;
          border-radius: 15px;
          text-align: center;
        }
      `}</style>

      <div className="signup-container">

        <div className="hero-section">
          <h1>Create Your Health<br />Account</h1>
          <p>Join our secure hospital platform for virtual consultations and seamless doctor communication.</p>
          <ul className="features">
            <li>100% Secure & Encrypted</li>
            <li>Verified Specialist Doctors</li>
            <li>24/7 Online Consultation</li>
          </ul>
        </div>

        <div className="form-section">
          <div className="form-card">

            <div className="form-header">
              <h2>Sign Up</h2>
            </div>

            {error && <div className="error-msg">{error}</div>}

            {!showOtp && (
              <form onSubmit={handleSubmit}>

                <div className="input-group">
                  <label>Email Address</label>
                  <input type="email" name="email" onChange={handleChange} required />
                </div>

                <div className="input-group">
                  <label>Mobile Number</label>
                  <input type="tel" name="phone" onChange={handleChange} required />
                </div>

                <div className="input-group">
                  <label>Create Password</label>
                  <input type="password" name="password" onChange={handleChange} required />
                </div>

                <div className="input-group">
                  <label>Date of Birth</label>
                  <input type="date" name="dob" onChange={handleChange} required />
                </div>

                <div className="input-group">
                  <label>Gender</label>
                  <select name="gender" onChange={handleChange} required>
                    <option value="">Select Gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>

                <div className="input-group">
                  <label>Full Name</label>
                  <input type="text" name="name" onChange={handleChange} required />
                </div>

                <label className="terms-container">
                  <input type="checkbox"
                    checked={acceptedTerms}
                    onChange={(e) => setAcceptedTerms(e.target.checked)} />
                  <span>I agree to the Terms & Privacy Policy</span>
                </label>

                <button type="submit" className="btn-create">
                  Create Account
                </button>

              </form>
            )}

            {showOtp && (
              <>
                <div className="input-group">
                  <label>Enter OTP</label>
                  <input maxLength="6"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)} />
                </div>

                <div className="otp-timer">
                  {timer > 0 ? `Resend OTP in ${timer}s`
                    : <button onClick={resendOtp}>Resend OTP</button>}
                </div>

                <button className="btn-create" onClick={verifyOtp}>
                  Verify OTP
                </button>
              </>
            )}

          </div>
        </div>

        {showSuccess && (
          <div className="success-overlay">
            <div className="success-box">
              <h3>Registration Successful 🎉</h3>
              <button className="btn-create"
                onClick={() => navigate("/booking")}>
                Continue to Booking
              </button>
            </div>
          </div>
        )}

      </div>
    </>
  );
}

export default Signup;