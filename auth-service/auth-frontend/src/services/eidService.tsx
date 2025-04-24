export async function decryptChallengeWithEid(challengeBase64: string) {
    const requestXml = `
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:ns="urn:be:fedict:eid:middleware:protocol:v1:0">
            <soapenv:Header/>
            <soapenv:Body>
                <ns:Decrypt>
                    <ns:Data>${challengeBase64}</ns:Data>
                </ns:Decrypt>
            </soapenv:Body>
        </soapenv:Envelope>`;

    const response = await fetch("https://127.0.0.1:24727/", {
        method: "POST",
        headers: {
            "Content-Type": "text/xml",
            "SOAPAction": "urn:be:fedict:eid:middleware:protocol:v1:0:Decrypt",
        },
        body: requestXml,
    });

    if (!response.ok) {
        throw new Error("Error during the request to the eID middleware.");
    }

    const responseText = await response.text();

    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(responseText, "text/xml");
    const decryptedValue = xmlDoc.getElementsByTagName("ns:Data")[0]?.textContent;

    if (!decryptedValue) {
        throw new Error("Impossible to decrypt the challenge.");
    }

    return decryptedValue;
}
