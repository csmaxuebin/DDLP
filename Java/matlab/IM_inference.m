function [pr_post,Trans_vertices,S] = IM_inference(p_prior, z, DeltaX, eps, T, A, vertices)
% pr is the probability vector of each possible value;
% pr(1,i)= Pr(  possible i  |  z  )
% Trans_A === A in IM_Release

    Delta=T*DeltaX';% each column is a possible answer;
    [d,N]=size(T);
    if(size(DeltaX,1)~=1 ) %if only one row, then stay the same. s=0;
        pr=sum(DeltaX);% a row like [ 0 1 0 0 1 1 0 0 1 ];
    else
        pr=DeltaX;
    end

    Trans_vertices=A*vertices;
%%compute the area of Trans_vertices
    x=Trans_vertices(1,:);
    y=Trans_vertices(2,:);
    n=length(x);
    s=0;
    for i=1:n-1;
        a=x(i)*y(i+1)-x(i+1)*y(i);
        s=s+a;
    end;
    S=1/2*s;
%%
    if sum(size(A))==2   % A==1;
       pr_post=p_prior;
       return;
    end

    col=1;
    for i=1:N
        if pr(1,i)==1
            %pr(1,i)=exp(- eps *  NormOfK( A*(z -Delta(:,col)),  Trans_vertices)      ); %the first used is wrong;lack the area
            pr(1,i)=((eps*eps)/(2*S))*exp(- eps *  NormOfK( A*(z -Delta(:,col)),  Trans_vertices)      );
            col=col+1;
        end
    end

    pr_post=pr.*p_prior;
    pr_post=pr_post/sum(pr_post);%Bayesian inference;

    if(sum(pr_post)==0 || sum(pr_post==NaN)>0)
        pr_post=p_prior;
    end

end
%------------------------------------over--------------------

%
% function [knorm]=NormOfK(x,V)
% %x is the point, V is the set of vertices; in isotropic space;
%
%             n=size(V,2);
%             %V=[V,V(:,1)];
%             scaleOfK=1;
%             iteration=0;
%             while (inpolygon(x(1),x(2),scaleOfK*V(1,:),scaleOfK*V(2,:))~=1) % not in V
%                 scaleOfK=scaleOfK+1;
%             end
%
%             while (inpolygon(x(1),x(2),scaleOfK*V(1,:),scaleOfK*V(2,:))==1) % not in V
%                 scaleOfK=scaleOfK-0.1;
%                 iteration=iteration+1;
%                 if iteration>200
%                     display(x);
%                 end
%             end
%
%     knorm=scaleOfK;
%
% end