import React from 'react';

const LoginBox = ({ imgSrc, altText, loginMethod }) => {
    return (
        <div className="box">
            <img src={imgSrc} alt={altText} className="icon" />
            <h3>Log in</h3>
            <h4><a href="/auth/login">{loginMethod}</a></h4>
            <p>This is a short description for the box.</p>
        </div>
    );
};

export default LoginBox;
