import React from 'react';

interface LoginBoxProps {
    imgSrc: string;
    altText: string;
    loginMethod: string;
    description: string;
}

const LoginBox: React.FC<LoginBoxProps> = ({ imgSrc, altText, loginMethod, description }) => {
    return (
        <div className="box">
            <img src={imgSrc} alt={altText} className="icon" />
            <h5>{loginMethod}</h5>
            <p>{description}</p>
        </div>
    );
};

export default LoginBox;
